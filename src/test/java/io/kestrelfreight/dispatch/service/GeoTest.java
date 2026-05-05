package io.kestrelfreight.dispatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeoTest {

    @Test
    void memphisToNashvilleIsAbout200Miles() {
        assertThat(Geo.haversineMiles(35.1495, -90.0490, 36.1627, -86.7816)).isCloseTo(196.0, within(3.0));
    }

    @Test
    void samePointIsZero() {
        assertThat(Geo.haversineMiles(35.0, -89.0, 35.0, -89.0)).isZero();
    }
}
