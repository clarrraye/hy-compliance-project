package com.example.hy_backend.service.impl;

import com.example.hy_backend.dto.FishingLogDTO;
import com.example.hy_backend.dto.FishingLogDTO.FishingLogSpeciesDTO;
import com.example.hy_backend.entity.*;
import com.example.hy_backend.mapper.*;
import com.example.hy_backend.service.FishingLogService;
import com.example.hy_backend.util.SpecValidator;
import com.example.hy_backend.vo.ChartDataVO;
import com.example.hy_backend.vo.FishingLogVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FishingLogServiceImpl implements FishingLogService {

    @Autowired
    private FishingLogMapper fishingLogMapper;
    @Autowired
    private FishingLogSpeciesMapper fishingLogSpeciesMapper;
    @Autowired
    private FishingBanRuleMapper fishingBanRuleMapper;
    @Autowired
    private SpeciesMapper speciesMapper;
    @Autowired
    private SeaAreaMapper seaAreaMapper;

    // 保存日志+自动合规校验（核心修复：新增主表合规状态汇总+更新）
    @Override
    @Transactional
    public Long saveFishingLog(FishingLogDTO logDTO) {
        // 1. 初始化主表日志对象（先不插入，先做合规校验）
        FishingLog log = new FishingLog();
        log.setUserId(logDTO.getUserId());
        log.setSeaId(logDTO.getSeaId());
        log.setFishingDate(LocalDate.parse(logDTO.getFishingDate()));
        log.setFishingGear(logDTO.getFishingGear());
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());

        // 新增：主表合规状态汇总变量（默认合规，只要有一个物种违规就置为违规）
        boolean isLogCompliant = true;
        StringBuilder logUncompliantReason = new StringBuilder();

        // 2. 处理物种明细+合规校验
        List<FishingLogSpecies> speciesList = new ArrayList<>();
        // 非空校验：避免空物种列表
        if (logDTO.getSpeciesList() == null || logDTO.getSpeciesList().isEmpty()) {
            throw new RuntimeException("捕捞物种列表不能为空！");
        }

        for (FishingLogSpeciesDTO speciesDTO : logDTO.getSpeciesList()) {
            FishingLogSpecies species = new FishingLogSpecies();
            // 先占位logId，插入主表后补值
            species.setLogId(log.getLogId());
            species.setSpeciesId(speciesDTO.getSpeciesId());
            species.setCatchNum(speciesDTO.getCatchNum());
            species.setCatchSpec(speciesDTO.getCatchSpec());

            // 单物种合规校验
            StringBuilder speciesUnReason = new StringBuilder();
            boolean isSpeciesCompliant = true;

            // 校验1：是否为禁捕物种
            Species speciesInfo = speciesMapper.selectById(speciesDTO.getSpeciesId());
            if (speciesInfo == null) {
                isSpeciesCompliant = false;
                speciesUnReason.append("物种信息不存在；");
            } else {
                if (speciesInfo.getIsCatch() == 0) {
                    isSpeciesCompliant = false;
                    speciesUnReason.append("该物种为禁捕物种；");
                }
            }

            // 校验2：是否在禁渔期
            FishingBanRule rule = fishingBanRuleMapper.selectOne(logDTO.getSeaId(), speciesDTO.getSpeciesId());
            if (rule != null) {
                try {
                    int startMonth = Integer.parseInt(rule.getBanStartTime().split("月")[0]);
                    int endMonth = Integer.parseInt(rule.getBanEndTime().split("月")[0]);
                    int fishingMonth = log.getFishingDate().getMonthValue();

                    if (fishingMonth >= startMonth && fishingMonth <= endMonth) {
                        isSpeciesCompliant = false;
                        speciesUnReason.append("当前海域该物种处于禁渔期（").append(rule.getBanStartTime()).append("-").append(rule.getBanEndTime()).append("）；");
                    }

                    // 校验3：规格是否达标（使用通用规格校验工具）
                    String specRequire = rule.getSpecRequire();
                    if (StringUtils.hasText(specRequire) && StringUtils.hasText(speciesDTO.getCatchSpec())) {
                        // 将规格标准化为公斤单位（如果原来是重量单位）
                        String standardizedSpec = standardizeWeightUnit(speciesDTO.getCatchSpec());
                        SpecValidator.ValidationResult specResult = SpecValidator.validate(specRequire, standardizedSpec);
                        if (!specResult.isCompliant()) {
                            isSpeciesCompliant = false;
                            speciesUnReason.append("捕捞").append(specResult.getMessage()).append("；");
                        }
                    }
                } catch (Exception e) {
                    isSpeciesCompliant = false;
                    speciesUnReason.append("禁渔期/规格校验异常：").append(e.getMessage()).append("；");
                }
            }

            // 设置单物种合规状态
            species.setIsCompliant(isSpeciesCompliant ? 1 : 0);
            species.setUncompliantReason(speciesUnReason.toString());
            speciesList.add(species);

            // 核心：汇总到主表 → 只要有一个物种违规，主表就违规
            if (!isSpeciesCompliant) {
                isLogCompliant = false;
                // 汇总违规原因（去重）
                if (logUncompliantReason.indexOf(speciesUnReason.toString()) == -1) {
                    logUncompliantReason.append(speciesUnReason);
                }
            }
        }

        // 3. 先插入主表（获取logId）
        fishingLogMapper.insert(log);
        Long logId = log.getLogId();
        // 补全物种明细的logId
        for (FishingLogSpecies species : speciesList) {
            species.setLogId(logId);
        }

        // 4. 批量插入物种明细
        if (!speciesList.isEmpty()) {
            fishingLogSpeciesMapper.batchInsert(speciesList);
        }

        // 5. 核心修复：更新主表的合规状态和违规原因
        log.setIsCompliant(isLogCompliant ? 1 : 0);
        log.setUncompliantReason(logUncompliantReason.length() > 0 ? logUncompliantReason.toString() : "无");
        fishingLogMapper.updateById(log);
        // 日志打印：验证更新结果
        System.out.println("主表合规状态更新：logId=" + logId + "，isCompliant=" + log.getIsCompliant() + "，违规原因=" + log.getUncompliantReason());

        return logId;
    }

    /**
     * 将重量单位标准化为公斤
     * @param originalSpec 原始规格字符串，如"500g"、"0.5kg"、"1斤"
     * @return 标准化后的规格字符串，如"0.5kg"
     */
    private String standardizeWeightUnit(String originalSpec) {
        if (originalSpec == null || originalSpec.trim().isEmpty()) {
            return originalSpec;
        }

        // 定义重量单位转换系数（转换为公斤）
        java.util.Map<String, Double> weightUnits = new java.util.HashMap<>();
        weightUnits.put("g", 0.001);     // 克转公斤
        weightUnits.put("kg", 1.0);      // 公斤本身
        weightUnits.put("公斤", 1.0);      // 中文公斤
        weightUnits.put("斤", 0.5);       // 斤转公斤
        weightUnits.put("千克", 1.0);     // 中文千克
        weightUnits.put("克", 0.001);     // 中文克

        // 提取数字和单位
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)");
        java.util.regex.Pattern unitPattern = java.util.regex.Pattern.compile("([a-zA-Z\u4e00-\u9fa5]+)");
        
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(originalSpec);
        java.util.regex.Matcher unitMatcher = unitPattern.matcher(originalSpec);

        if (numberMatcher.find() && unitMatcher.find()) {
            double value = Double.parseDouble(numberMatcher.group(1));
            String unit = unitMatcher.group(1);
            
            // 如果是重量单位，则转换为公斤
            if (weightUnits.containsKey(unit)) {
                double kgValue = value * weightUnits.get(unit);
                return String.format("%.3f", kgValue) + "kg";
            }
        }
        
        // 如果不是重量单位，直接返回原值
        return originalSpec;
    }

    // 原有getLogList方法：保留不变
    @Override
    public List<FishingLogVO> getLogList(Integer userId, String fishingDate, Integer seaId, Integer isCompliant, Integer pageNum, Integer pageSize) {
        int start = (pageNum - 1) * pageSize;
        return fishingLogMapper.selectLogList(userId, fishingDate, seaId, isCompliant, start, pageSize);
    }

    // 原有getLogCount方法：保留不变
    @Override
    public Integer getLogCount(Integer userId, String fishingDate, Integer seaId, Integer isCompliant) {
        return fishingLogMapper.selectLogCount(userId, fishingDate, seaId, isCompliant);
    }

    // 修改getChartData方法：支持时间范围筛选和鱼种重量统计
    @Override
    public ChartDataVO getChartData(Integer userId, String timeRange) {
        // 根据时间范围构建查询条件
        String dateCondition = buildDateCondition(timeRange);
        
        // 查询指定时间范围内的日志
        List<FishingLogVO> logList = fishingLogMapper.selectLogListWithDateCondition(userId, dateCondition, 0, Integer.MAX_VALUE);

        if (logList == null) {
            logList = new ArrayList<>();
        }
        logList = logList.stream().filter(Objects::nonNull).collect(Collectors.toList());

        int compliantCount = 0, uncompliantCount = 0;
        // 统计各鱼种重量
        Map<String, Double> speciesWeightMap = new HashMap<>();
        
        for (FishingLogVO log : logList) {
            if (log.getIsCompliant() == 1) compliantCount++;
            else uncompliantCount++;
            
            // 从日志ID查询对应的物种信息和重量
            List<FishingLogSpecies> speciesList = fishingLogSpeciesMapper.selectByLogId(log.getLogId());
            for (FishingLogSpecies species : speciesList) {
                Species speciesInfo = speciesMapper.selectById(species.getSpeciesId());
                String speciesName = speciesInfo != null ? speciesInfo.getSpeciesName() : "未知鱼种";
                
                // 解析规格，如果是重量单位则累加
                double weight = parseWeightFromSpec(species.getCatchSpec());
                if (weight > 0) {
                    speciesWeightMap.put(speciesName, speciesWeightMap.getOrDefault(speciesName, 0.0) + weight);
                }
            }
        }

        ChartDataVO vo = new ChartDataVO();
        vo.setCompliantCount(compliantCount);
        vo.setUncompliantCount(uncompliantCount);

        // 设置鱼种重量数据
        List<ChartDataVO.SpeciesCatchVO> speciesWeightList = new ArrayList<>();
        speciesWeightMap.forEach((name, weight) -> {
            ChartDataVO.SpeciesCatchVO s = new ChartDataVO.SpeciesCatchVO();
            s.setName(name);
            s.setValue(weight);
            speciesWeightList.add(s);
        });
        vo.setSpeciesWeightData(speciesWeightList);

        return vo;
    }

    /**
     * 构建日期查询条件
     * @param timeRange 时间范围：day(今日), week(近一周), month(近一个月), year(近一年)
     * @return SQL日期条件
     */
    private String buildDateCondition(String timeRange) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (timeRange) {
            case "day":
                startDate = endDate;
                break;
            case "week":
                startDate = endDate.minusWeeks(1);
                break;
            case "month":
                startDate = endDate.minusMonths(1);
                break;
            case "year":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.minusMonths(1); // 默认近一个月
        }
        
        return " AND fishing_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
    }
    
    /**
     * 从规格字符串中解析重量值（公斤）
     * @param spec 规格字符串，如"500g", "0.5kg", "1斤"
     * @return 重量值（公斤）
     */
    private double parseWeightFromSpec(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return 0.0;
        }
        
        // 使用之前添加的单位标准化方法
        String standardizedSpec = standardizeWeightUnit(spec);
        
        // 提取数值部分
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)");
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(standardizedSpec);
        
        if (numberMatcher.find()) {
            return Double.parseDouble(numberMatcher.group(1));
        }
        
        return 0.0;
    }

    public void exportReport(Integer userId, HttpServletResponse response) {
    try {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("捕捞合规自查报告");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"日志ID", "捕捞海域", "捕捞日期", "渔具类型", "鱼种名称", "捕捞规格", "合规状态", "违规原因"};
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        List<FishingLogVO> logList = fishingLogMapper.selectLogList(userId, null, null, null, 0, Integer.MAX_VALUE);
        if (logList == null) {
            logList = new ArrayList<>();
        }
        logList = logList.stream().filter(Objects::nonNull).collect(Collectors.toList());

        int rowIndex = 1;
        for (FishingLogVO log : logList) {
            // 查询该日志关联的物种信息
            List<FishingLogSpecies> speciesList = fishingLogSpeciesMapper.selectByLogId(log.getLogId());
            
            if (speciesList != null && !speciesList.isEmpty()) {
                // 如果有物种信息，为每个物种创建一行
                for (int j = 0; j < speciesList.size(); j++) {
                    FishingLogSpecies species = speciesList.get(j);
                    Species speciesInfo = speciesMapper.selectById(species.getSpeciesId());
                    
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(log.getLogId());
                    row.createCell(1).setCellValue(log.getSeaName());
                    row.createCell(2).setCellValue(log.getFishingDate().toString());
                    row.createCell(3).setCellValue(log.getFishingGear());
                    row.createCell(4).setCellValue(speciesInfo != null ? speciesInfo.getSpeciesName() : "未知鱼种");
                    row.createCell(5).setCellValue(species.getCatchSpec() != null ? species.getCatchSpec() : "");
                    row.createCell(6).setCellValue(log.getIsCompliant() == 1 ? "合规" : "违规");
                    row.createCell(7).setCellValue(log.getUncompliantReason() == null ? "无" : log.getUncompliantReason());
                }
            } else {
                // 如果没有物种信息，创建一行空的物种信息
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(log.getLogId());
                row.createCell(1).setCellValue(log.getSeaName());
                row.createCell(2).setCellValue(log.getFishingDate().toString());
                row.createCell(3).setCellValue(log.getFishingGear());
                row.createCell(4).setCellValue(""); // 鱼种名称为空
                row.createCell(5).setCellValue(""); // 捕捞规格为空
                row.createCell(6).setCellValue(log.getIsCompliant() == 1 ? "合规" : "违规");
                row.createCell(7).setCellValue(log.getUncompliantReason() == null ? "无" : log.getUncompliantReason());
            }
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("捕捞合规自查报告_" + LocalDateTime.now().toString().replace(":", "-") + ".xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        OutputStream os = response.getOutputStream();
        workbook.write(os);
        os.flush();
        os.close();
        workbook.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    // 新增：删除日志（无需依赖任何其他Mapper）
    @Override
    @Transactional // 事务保证：删明细和主表要么都成功，要么都回滚
    public void deleteFishingLog(Long logId) {
        // 1. 校验日志ID有效性
        if (logId == null || logId <= 0) {
            throw new RuntimeException("日志ID不能为空且必须大于0！");
        }

        // 2. 第一步：删除物种明细（先删子表，避免外键报错）
        fishingLogMapper.deleteLogSpeciesByLogId(logId);

        // 3. 第二步：删除日志主表
        int deleteCount = fishingLogMapper.deleteLogById(logId);
        if (deleteCount == 0) {
            throw new RuntimeException("日志不存在，删除失败！");
        }

        System.out.println("日志删除成功，logId：" + logId);
    }
        /**
     * 获取所有用户的捕捞日志（管理员功能）
     */
    @Override
    public List<FishingLog> getAllLogs(Integer page, Integer size, String fishingDate, Integer seaId, Integer isCompliant) {
        Integer offset = (page - 1) * size;
        return fishingLogMapper.selectAllLogs(offset, size, fishingDate, seaId, isCompliant);
    }
    /**
     * 获取所有捕捞日志总数（管理员功能）
     */
    @Override
    public Integer getAllLogsCount(String fishingDate, Integer seaId, Integer isCompliant) {
        return fishingLogMapper.selectAllLogsCount(fishingDate, seaId, isCompliant);
    }
    /**
     * 删除捕捞日志（管理员功能）
     */
    @Override
    public boolean deleteLog(Long logId) {
        if (logId == null || logId <= 0) {
            return false;
        }
        
        try {
            // 先删除物种明细
            fishingLogMapper.deleteLogSpeciesByLogId(logId);
            // 再删除主表
            int deleteCount = fishingLogMapper.deleteLogById(logId);
            return deleteCount > 0;
        } catch (Exception e) {
            System.out.println("删除日志失败：" + e.getMessage());
            return false;
        }
    }
        /**
     * 获取所有用户的捕捞日志VO（管理员功能）
     */
    @Override
    public List<FishingLogVO> getAllLogsVO(Integer page, Integer size, String fishingDate, Integer seaId, Integer isCompliant) {
        Integer offset = (page - 1) * size;
        return fishingLogMapper.selectAllLogsVO(offset, size, fishingDate, seaId, isCompliant);
    }
}