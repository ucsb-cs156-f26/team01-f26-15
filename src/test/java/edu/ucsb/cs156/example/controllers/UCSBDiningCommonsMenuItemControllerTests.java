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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository menuItemRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems/all")).andExpect(status().isOk());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitems/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Salad")
                .param("station", "Entrees")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitems/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Salad")
                .param("station", "Entrees")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_items() throws Exception {
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Chicken Salad")
            .station("Entrees")
            .build();

    UCSBDiningCommonsMenuItem item2 =
        UCSBDiningCommonsMenuItem.builder()
            .id(2L)
            .diningCommonsCode("carrillo")
            .name("Pizza")
            .station("Pizza")
            .build();

    List<UCSBDiningCommonsMenuItem> expectedItems = Arrays.asList(item1, item2);

    when(menuItemRepository.findAll()).thenReturn(expectedItems);

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseJson = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseJson);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_empty_list() throws Exception {
    when(menuItemRepository.findAll()).thenReturn(List.of());

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemRepository, times(1)).findAll();
    assertEquals("[]", response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_item() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Salad")
            .station("Entrees")
            .build();

    when(menuItemRepository.save(eq(item))).thenReturn(item);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitems/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Chicken Salad")
                    .param("station", "Entrees")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemRepository, times(1)).save(item);

    String expectedJson = mapper.writeValueAsString(item);
    String responseJson = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseJson);
  }
}
