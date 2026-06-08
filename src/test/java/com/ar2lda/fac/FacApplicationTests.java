package com.ar2lda.fac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class FacApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void serveWorkspaceInicial() throws Exception {
		mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("FAC Workspace")))
				.andExpect(content().string(containsString("comercial-detail")))
				.andExpect(content().string(containsString("financeiro-detail")));

		mockMvc.perform(get("/app.js"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("/documentos-comerciais")))
				.andExpect(content().string(containsString("renderComercialDetail")))
				.andExpect(content().string(containsString("renderFinanceiroDetail")));
	}
}
