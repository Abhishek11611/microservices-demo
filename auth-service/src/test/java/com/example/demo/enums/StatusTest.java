package com.example.demo.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusTest {

    @Test
    void fromValue_returnsActiveForY() {
        assertThat(Status.fromValue("Y")).isEqualTo(Status.ACTIVE);
    }

    @Test
    void fromValue_returnsInactiveForLowercaseN() {
        assertThat(Status.fromValue("n")).isEqualTo(Status.INACTIVE);
    }

    @Test
    void fromValue_returnsNullForNullOrBlankValues() {
        assertThat(Status.fromValue(null)).isNull();
        assertThat(Status.fromValue("")).isNull();
        assertThat(Status.fromValue("   ")).isNull();
    }

    @Test
    void fromValue_withUnknownValue_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Status.fromValue("X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown status value: X");
    }

    @Test
    void statusConverter_convertsEnumToDatabaseValue() {
        Status.StatusConverter converter = new Status.StatusConverter();

        assertThat(converter.convertToDatabaseColumn(Status.ACTIVE)).isEqualTo('Y');
        assertThat(converter.convertToDatabaseColumn(Status.INACTIVE)).isEqualTo('N');
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void statusConverter_convertsDatabaseValueToEnum() {
        Status.StatusConverter converter = new Status.StatusConverter();

        assertThat(converter.convertToEntityAttribute('Y')).isEqualTo(Status.ACTIVE);
        assertThat(converter.convertToEntityAttribute('n')).isEqualTo(Status.INACTIVE);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void statusConverter_withUnknownDatabaseValue_throwsIllegalArgumentException() {
        Status.StatusConverter converter = new Status.StatusConverter();

        assertThatThrownBy(() -> converter.convertToEntityAttribute('X'))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
