package com.scholartrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiValidationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String studentJson(String email, String income, String marks) {
        return """
                {"name":"API Student","email":"%s","phone":"9876543210",
                 "annualIncome":%s,"marks":%s,"course":"B.E. Computer Science","year":2}
                """.formatted(email, income, marks);
    }

    @Test
    void invalidMarksAreRejected() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("marks1@example.com", "100000", "150")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.marks").exists());

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("marks2@example.com", "100000", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.marks").exists());
    }

    @Test
    void invalidIncomeIsRejected() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("income@example.com", "-100", "80")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.annualIncome").exists());
    }

    @Test
    void invalidSchemeIsRejected() throws Exception {
        mockMvc.perform(post("/api/schemes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"incomeLimit\":0,\"minimumMarks\":120}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.incomeLimit").exists())
                .andExpect(jsonPath("$.errors.minimumMarks").exists());
    }

    @Test
    void prematureDisbursementReturnsBusinessRuleError() throws Exception {
        long studentId = createAndGetId("/api/students", studentJson("flow@example.com", "100000", "90"));
        long schemeId = createAndGetId("/api/schemes",
                "{\"name\":\"Flow Scheme\",\"description\":\"d\",\"incomeLimit\":300000,\"minimumMarks\":60,\"status\":\"ACTIVE\"}");
        long applicationId = createAndGetId("/api/applications",
                "{\"studentId\":" + studentId + ",\"schemeId\":" + schemeId + "}");

        mockMvc.perform(put("/api/applications/" + applicationId + "/disburse"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Rule Violation"))
                .andExpect(jsonPath("$.message")
                        .value("Disbursement cannot be completed until verification is approved."));
    }

    private long createAndGetId(String url, String json) throws Exception {
        MvcResult result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
