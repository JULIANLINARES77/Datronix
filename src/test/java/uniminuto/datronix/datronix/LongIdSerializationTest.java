package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import uniminuto.datronix.dto.ProductoDTO;

class LongIdSerializationTest {
    @Test
    void serializesLargeProductIdAsExactJsonString() throws Exception {
        ProductoDTO producto = ProductoDTO.builder()
                .idProducto(9007199254740993L)
                .build();

        String json = new ObjectMapper().writeValueAsString(producto);

        assertEquals("\"9007199254740993\"", new ObjectMapper().readTree(json).get("idProducto").toString());
    }
}
