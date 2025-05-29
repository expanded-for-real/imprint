package com.imprint;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ConstantsTest {
    
    @Test
    void shouldHaveCorrectMagicByte() {
        assertThat(Constants.MAGIC).isEqualTo((byte) 0x49);
    }
    
    @Test
    void shouldHaveCorrectVersion() {
        assertThat(Constants.VERSION).isEqualTo((byte) 0x01);
    }
}
