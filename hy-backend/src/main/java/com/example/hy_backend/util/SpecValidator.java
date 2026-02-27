package com.example.hy_backend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecValidator {

    // 定义单位类型枚举
    enum UnitType { LENGTH, WEIGHT, OTHER }

    public static class ValidationResult {
        private boolean compliant;
        private String message;

        public ValidationResult(boolean compliant, String message) {
            this.compliant = compliant;
            this.message = message;
        }

        public boolean isCompliant() {
            return compliant;
        }

        public String getMessage() {
            return message;
        }
    }

    public static ValidationResult validate(String specRequire, String actualSpec) {
        if (specRequire == null || specRequire.trim().isEmpty()) {
            return new ValidationResult(true, "无规格要求");
        }
        if (actualSpec == null || actualSpec.trim().isEmpty()) {
            return new ValidationResult(false, "未填写实际规格");
        }

        specRequire = specRequire.trim();
        actualSpec = actualSpec.trim();

        if (specRequire.contains("≥") || specRequire.contains(">=")) {
            return validateMin(specRequire, actualSpec);
        }

        if (specRequire.contains("≤") || specRequire.contains("<=")) {
            return validateMax(specRequire, actualSpec);
        }

        if (specRequire.contains("-") && !specRequire.contains("月")) {
            return validateRange(specRequire, actualSpec);
        }

        if (specRequire.contains(">") && !specRequire.contains(">=")) {
            return validateGreaterThan(specRequire, actualSpec);
        }

        if (specRequire.contains("<") && !specRequire.contains("<=")) {
            return validateLessThan(specRequire, actualSpec);
        }

        return new ValidationResult(true, "规格要求格式无法识别，默认通过");
    }

    private static ValidationResult validateMin(String specRequire, String actualSpec) {
        Pattern pattern = Pattern.compile("[≥>=]+\\s*(\\d+(\\.\\d+)?)\\s*(cm|mm|m|g|kg|克|厘米|米|公斤|斤)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(specRequire);
        
        if (!matcher.find()) {
            return new ValidationResult(true, "最小值规格格式无法解析，默认通过");
        }

        double requiredValue = Double.parseDouble(matcher.group(1));
        String requiredUnit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : "cm";

        double actualValue = extractNumber(actualSpec);
        String actualUnit = extractUnit(actualSpec);

        if (getUnitType(requiredUnit) != getUnitType(actualUnit)) {
            return new ValidationResult(false, 
                String.format("单位类型不匹配（规则要求:%s，实际填写:%s），请检查填写单位", requiredUnit, actualUnit));
        }

        requiredValue = convertToBaseUnit(requiredValue, requiredUnit);
        actualValue = convertToBaseUnit(actualValue, actualUnit);

        if (actualValue >= requiredValue) {
            return new ValidationResult(true, 
                String.format("规格达标（要求≥%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        } else {
            return new ValidationResult(false, 
                String.format("规格不达标（要求≥%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        }
    }

    private static ValidationResult validateMax(String specRequire, String actualSpec) {
        Pattern pattern = Pattern.compile("[≤<=]+\\s*(\\d+(\\.\\d+)?)\\s*(cm|mm|m|g|kg|克|厘米|米|公斤|斤)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(specRequire);
        
        if (!matcher.find()) {
            return new ValidationResult(true, "最大值规格格式无法解析，默认通过");
        }

        double requiredValue = Double.parseDouble(matcher.group(1));
        String requiredUnit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : "cm";

        double actualValue = extractNumber(actualSpec);
        String actualUnit = extractUnit(actualSpec);

        if (getUnitType(requiredUnit) != getUnitType(actualUnit)) {
            return new ValidationResult(false, 
                String.format("单位类型不匹配（规则要求:%s，实际填写:%s），请检查填写单位", requiredUnit, actualUnit));
        }

        requiredValue = convertToBaseUnit(requiredValue, requiredUnit);
        actualValue = convertToBaseUnit(actualValue, actualUnit);

        if (actualValue <= requiredValue) {
            return new ValidationResult(true, 
                String.format("规格达标（要求≤%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        } else {
            return new ValidationResult(false, 
                String.format("规格超标（要求≤%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        }
    }

    private static ValidationResult validateRange(String specRequire, String actualSpec) {
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(cm|mm|m|g|kg|克|厘米|米|公斤|斤)?\\s*-\\s*(\\d+(\\.\\d+)?)\\s*(cm|mm|m|g|kg|克|厘米|米|公斤|斤)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(specRequire);
        
        if (!matcher.find()) {
            return new ValidationResult(true, "范围规格格式无法解析，默认通过");
        }

        double minValue = Double.parseDouble(matcher.group(1));
        String minUnit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : "cm";
        double maxValue = Double.parseDouble(matcher.group(4));
        String maxUnit = matcher.group(6) != null ? matcher.group(6).toLowerCase() : minUnit;

        double actualValue = extractNumber(actualSpec);
        String actualUnit = extractUnit(actualSpec);

        if (getUnitType(minUnit) != getUnitType(actualUnit)) {
            return new ValidationResult(false, 
                String.format("单位类型不匹配（规则要求:%s，实际填写:%s），请检查填写单位", minUnit, actualUnit));
        }

        minValue = convertToBaseUnit(minValue, minUnit);
        maxValue = convertToBaseUnit(maxValue, maxUnit);
        actualValue = convertToBaseUnit(actualValue, actualUnit);

        if (actualValue >= minValue && actualValue <= maxValue) {
            return new ValidationResult(true, 
                String.format("规格在允许范围内（要求%.1f-%.1f%s，实际%.1f%s）", 
                    minValue, maxValue, minUnit, actualValue, actualUnit));
        } else {
            return new ValidationResult(false, 
                String.format("规格超出允许范围（要求%.1f-%.1f%s，实际%.1f%s）", 
                    minValue, maxValue, minUnit, actualValue, actualUnit));
        }
    }

    private static ValidationResult validateGreaterThan(String specRequire, String actualSpec) {
        Pattern pattern = Pattern.compile(">\\s*(\\d+(\\.\\d+)?)\\s*(cm|mm|m|g|kg|克|厘米|米|公斤|斤)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(specRequire);
        
        if (!matcher.find()) {
            return new ValidationResult(true, "大于规格格式无法解析，默认通过");
        }

        double requiredValue = Double.parseDouble(matcher.group(1));
        String requiredUnit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : "cm";

        double actualValue = extractNumber(actualSpec);
        String actualUnit = extractUnit(actualSpec);

        if (getUnitType(requiredUnit) != getUnitType(actualUnit)) {
            return new ValidationResult(false, 
                String.format("单位类型不匹配（规则要求:%s，实际填写:%s），请检查填写单位", requiredUnit, actualUnit));
        }

        requiredValue = convertToBaseUnit(requiredValue, requiredUnit);
        actualValue = convertToBaseUnit(actualValue, actualUnit);

        if (actualValue > requiredValue) {
            return new ValidationResult(true, 
                String.format("规格达标（要求>%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        } else {
            return new ValidationResult(false, 
                String.format("规格不达标（要求>%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        }
    }

    private static ValidationResult validateLessThan(String specRequire, String actualSpec) {
        Pattern pattern = Pattern.compile("<\\s*(\\d+(\\.\\d+)?)\\s*(cm|mm|m|g|kg|克|厘米|米|公斤|斤)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(specRequire);
        
        if (!matcher.find()) {
            return new ValidationResult(true, "小于规格格式无法解析，默认通过");
        }

        double requiredValue = Double.parseDouble(matcher.group(1));
        String requiredUnit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : "cm";

        double actualValue = extractNumber(actualSpec);
        String actualUnit = extractUnit(actualSpec);

        if (getUnitType(requiredUnit) != getUnitType(actualUnit)) {
            return new ValidationResult(false, 
                String.format("单位类型不匹配（规则要求:%s，实际填写:%s），请检查填写单位", requiredUnit, actualUnit));
        }

        requiredValue = convertToBaseUnit(requiredValue, requiredUnit);
        actualValue = convertToBaseUnit(actualValue, actualUnit);

        if (actualValue < requiredValue) {
            return new ValidationResult(true, 
                String.format("规格达标（要求<%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        } else {
            return new ValidationResult(false, 
                String.format("规格超标（要求<%.1f%s，实际%.1f%s）", 
                    requiredValue, requiredUnit, actualValue, actualUnit));
        }
    }

    private static double extractNumber(String spec) {
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(spec);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0;
    }

    private static String extractUnit(String spec) {
        Pattern pattern = Pattern.compile("(cm|mm|m|g|kg|克|厘米|米|公斤|斤)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(spec);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return "cm";
    }

    private static UnitType getUnitType(String unit) {
        if (unit.matches("(?i)(cm|mm|m|厘米|米|毫米)")) return UnitType.LENGTH;
        if (unit.matches("(?i)(g|kg|斤|公斤|克|千克)")) return UnitType.WEIGHT;
        return UnitType.OTHER;
    }

    private static double convertToBaseUnit(double value, String unit) {
        switch (unit.toLowerCase()) {
            case "mm":
            case "毫米":
                return value / 10.0;
            case "m":
            case "米":
                return value * 100.0;
            case "cm":
            case "厘米":
                return value;
            case "kg":
            case "公斤":
            case "千克":
                return value * 1000.0;
            case "斤":
                return value * 500.0;
            case "g":
            case "克":
                return value;
            default:
                return value;
        }
    }
}
