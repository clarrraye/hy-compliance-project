package com.example.hy_backend.util;

import com.example.hy_backend.util.SpecValidator.ValidationResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecValidatorTest {

    @Test
    void testValidateMinValue() {
        ValidationResult result1 = SpecValidator.validate("≥10cm", "15cm");
        assertTrue(result1.isCompliant());
        assertTrue(result1.getMessage().contains("达标"));

        ValidationResult result2 = SpecValidator.validate("≥10cm", "8cm");
        assertFalse(result2.isCompliant());
        assertTrue(result2.getMessage().contains("不达标"));

        ValidationResult result3 = SpecValidator.validate(">=500g", "600g");
        assertTrue(result3.isCompliant());

        ValidationResult result4 = SpecValidator.validate("≥1kg", "800g");
        assertFalse(result4.isCompliant());
    }

    @Test
    void testValidateMaxValue() {
        ValidationResult result1 = SpecValidator.validate("≤50cm", "40cm");
        assertTrue(result1.isCompliant());

        ValidationResult result2 = SpecValidator.validate("≤50cm", "60cm");
        assertFalse(result2.isCompliant());
        assertTrue(result2.getMessage().contains("超标"));

        ValidationResult result3 = SpecValidator.validate("<=2kg", "1.5kg");
        assertTrue(result3.isCompliant());
    }

    @Test
    void testValidateRange() {
        ValidationResult result1 = SpecValidator.validate("20-30cm", "25cm");
        assertTrue(result1.isCompliant());
        assertTrue(result1.getMessage().contains("范围内"));

        ValidationResult result2 = SpecValidator.validate("20-30cm", "15cm");
        assertFalse(result2.isCompliant());
        assertTrue(result2.getMessage().contains("超出"));

        ValidationResult result3 = SpecValidator.validate("20-30cm", "35cm");
        assertFalse(result3.isCompliant());

        ValidationResult result4 = SpecValidator.validate("500-1000g", "750g");
        assertTrue(result4.isCompliant());
    }

    @Test
    void testValidateGreaterThan() {
        ValidationResult result1 = SpecValidator.validate(">10cm", "15cm");
        assertTrue(result1.isCompliant());

        ValidationResult result2 = SpecValidator.validate(">10cm", "10cm");
        assertFalse(result2.isCompliant());

        ValidationResult result3 = SpecValidator.validate(">10cm", "8cm");
        assertFalse(result3.isCompliant());
    }

    @Test
    void testValidateLessThan() {
        ValidationResult result1 = SpecValidator.validate("<50cm", "40cm");
        assertTrue(result1.isCompliant());

        ValidationResult result2 = SpecValidator.validate("<50cm", "50cm");
        assertFalse(result2.isCompliant());

        ValidationResult result3 = SpecValidator.validate("<50cm", "60cm");
        assertFalse(result3.isCompliant());
    }

    @Test
    void testUnitConversion() {
        ValidationResult result1 = SpecValidator.validate("≥100mm", "10cm");
        assertTrue(result1.isCompliant());

        ValidationResult result2 = SpecValidator.validate("≥1m", "100cm");
        assertTrue(result2.isCompliant());

        ValidationResult result3 = SpecValidator.validate("≥1kg", "1000g");
        assertTrue(result3.isCompliant());

        ValidationResult result4 = SpecValidator.validate("≥2斤", "1000g");
        assertTrue(result4.isCompliant());
    }

    @Test
    void testChineseUnit() {
        ValidationResult result1 = SpecValidator.validate("≥10厘米", "15厘米");
        assertTrue(result1.isCompliant());

        ValidationResult result2 = SpecValidator.validate("≥500克", "600克");
        assertTrue(result2.isCompliant());

        ValidationResult result3 = SpecValidator.validate("≥1公斤", "1000克");
        assertTrue(result3.isCompliant());
    }

    @Test
    void testEdgeCases() {
        ValidationResult result1 = SpecValidator.validate(null, "10cm");
        assertTrue(result1.isCompliant());
        assertTrue(result1.getMessage().contains("无规格要求"));

        ValidationResult result2 = SpecValidator.validate("", "10cm");
        assertTrue(result2.isCompliant());

        ValidationResult result3 = SpecValidator.validate("≥10cm", null);
        assertFalse(result3.isCompliant());
        assertTrue(result3.getMessage().contains("未填写"));

        ValidationResult result4 = SpecValidator.validate("≥10cm", "");
        assertFalse(result4.isCompliant());

        ValidationResult result5 = SpecValidator.validate("未知格式", "10cm");
        assertTrue(result5.isCompliant());
        assertTrue(result5.getMessage().contains("无法识别"));
    }

    @Test
    void testDecimalValues() {
        ValidationResult result1 = SpecValidator.validate("≥10.5cm", "10.6cm");
        assertTrue(result1.isCompliant());

        ValidationResult result2 = SpecValidator.validate("≥10.5cm", "10.4cm");
        assertFalse(result2.isCompliant());

        ValidationResult result3 = SpecValidator.validate("10.5-20.5cm", "15.5cm");
        assertTrue(result3.isCompliant());
    }
}
