package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uniminuto.datronix.service.BodegaService;

@SpringBootTest
class DatronixApplicationTests {

    @Autowired
    private BodegaService bodegaService;

    @Test
    void bodegaServiceReadsRemissionsFromConfiguredDatabase() {
        assertNotNull(bodegaService.listarRemisionesPendientes());
    }

}
