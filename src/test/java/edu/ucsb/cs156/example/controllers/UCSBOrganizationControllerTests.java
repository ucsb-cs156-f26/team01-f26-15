package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;
  @MockitoBean UserRepository userRepository;

  // Test for method allOrganizations()
  @WithMockUser(roles = {"USER"})
  @Test
  public void test_allOrganizations() throws Exception {
    UCSBOrganization org1 =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(true)
            .build();

    UCSBOrganization org2 =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findAll()).thenReturn(Arrays.asList(org1, org2));

    mockMvc
        .perform(get("/api/UCSBOrganization/all"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].orgCode").value("ZPR"))
        .andExpect(jsonPath("$[0].orgTranslationShort").value("ZETA PHI RHO"))
        .andExpect(jsonPath("$[0].orgTranslation").value("ZETA PHI RHO"))
        .andExpect(jsonPath("$[0].inactive").value(true))
        .andExpect(jsonPath("$[1].orgCode").value("SKY"))
        .andExpect(jsonPath("$[1].orgTranslationShort").value("SKYDIVING CLUB"))
        .andExpect(jsonPath("$[1].orgTranslation").value("SKYDIVING CLUB AT UCSB"))
        .andExpect(jsonPath("$[1].inactive").value(false));
  }

  // Authorization tests for /api/UCSBOrganization/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/UCSBOrganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/UCSBOrganization/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ZPR")
                .param("orgTranslationShort", "ZETA PHI RHO")
                .param("orgTranslation", "ZETA PHI RHO")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ZPR")
                .param("orgTranslationShort", "ZETA PHI RHO")
                .param("orgTranslation", "ZETA PHI RHO")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(sky))).thenReturn(sky);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post")
                    .param("orgCode", "SKY")
                    .param("orgTranslationShort", "SKYDIVING CLUB")
                    .param("orgTranslation", "SKYDIVING CLUB AT UCSB")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(sky);
    String expectedJson = mapper.writeValueAsString(sky);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Get Mapping of where you get single record by orgCode when it doesn't exist
  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("orgCode", "ZPR"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("ZPR"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id ZPR not found", json.get("message"));
  }

  // Get Mapping of where you get single record by orgCode when it doesn't exist
  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

    // arrange

    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(organization));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("orgCode", "ZPR"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    String responseString = response.getResponse().getContentAsString();
    String expectedJson = mapper.writeValueAsString(organization);
    assertEquals(expectedJson, responseString);
  }
}
