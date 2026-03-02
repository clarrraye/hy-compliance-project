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
                        SpecValidator.ValidationResult specResult = SpecValidator.validate(specRequire, speciesDTO.getCatchSpec());
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

    // 原有getChartData方法：保留不变
    @Override
    public ChartDataVO getChartData(Integer userId, String timeRange) {
        ChartDataVO vo = new ChartDataVO();
        List<FishingLogVO> logList = fishingLogMapper.selectLogList(userId, null, null, null, 0, Integer.MAX_VALUE);

        if (logList == null) {
            logList = new ArrayList<>();
        }
        logList = logList.stream().filter(Objects::nonNull).collect(Collectors.toList());

        int compliantCount = 0, uncompliantCount = 0;
        Map<String, Integer> speciesCatchMap = new HashMap<>();
        Map<String, Integer> seaCompliantMap = new HashMap<>();
        Map<String, Integer> seaTotalMap = new HashMap<>();

        for (FishingLogVO log : logList) {
            if (log.getIsCompliant() == 1) compliantCount++;
            else uncompliantCount++;

            String seaName = log.getSeaName() == null ? "未配置海域" : log.getSeaName();
            seaTotalMap.put(seaName, seaTotalMap.getOrDefault(seaName, 0) + 1);
            if (log.getIsCompliant() == 1) {
                seaCompliantMap.put(seaName, seaCompliantMap.getOrDefault(seaName, 0) + 1);
            }

            String speciesName = "物种-" + log.getLogId();
            speciesCatchMap.put(speciesName, speciesCatchMap.getOrDefault(speciesName, 0) + 1);
        }

        vo.setCompliantCount(compliantCount);
        vo.setUncompliantCount(uncompliantCount);

        List<ChartDataVO.SpeciesCatchVO> speciesList = new ArrayList<>();
        speciesCatchMap.forEach((name, value) -> {
            ChartDataVO.SpeciesCatchVO s = new ChartDataVO.SpeciesCatchVO();
            s.setName(name);
            s.setValue(value);
            speciesList.add(s);
        });
        vo.setSpeciesCatchData(speciesList);

        List<ChartDataVO.SeaCompliantRateVO> seaList = new ArrayList<>();
        seaTotalMap.forEach((name, total) -> {
            ChartDataVO.SeaCompliantRateVO s = new ChartDataVO.SeaCompliantRateVO();
            s.setName(name);
            double rate = total == 0 ? 0 : (seaCompliantMap.getOrDefault(name, 0) * 100.0) / total;
            s.setRate(Math.round(rate * 100) / 100.0);
            seaList.add(s);
        });
        vo.setCompliantRateBySea(seaList);

        return vo;
    }

    // 原有exportReport方法：保留不变
    @Override
    public void exportReport(Integer userId, HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("捕捞合规自查报告");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"日志ID", "捕捞海域", "捕捞日期", "渔具类型", "合规状态", "违规原因"};
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

            for (int i = 0; i < logList.size(); i++) {
                FishingLogVO log = logList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(log.getLogId());
                row.createCell(1).setCellValue(log.getSeaName());
                row.createCell(2).setCellValue(log.getFishingDate().toString());
                row.createCell(3).setCellValue(log.getFishingGear());
                row.createCell(4).setCellValue(log.getIsCompliant() == 1 ? "合规" : "违规");
                row.createCell(5).setCellValue(log.getUncompliantReason() == null ? "无" : log.getUncompliantReason());
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