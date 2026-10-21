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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitems").param("id", "1"))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_item_exists() throws Exception {
    // Arrange
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Chicken Salad")
            .station("Entrees")
            .build();

    when(menuItemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

    // Act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems").param("id", "1"))
            .andExpect(status().isOk())
            .andReturn();

    // Assert
    verify(menuItemRepository, times(1)).findById(eq(1L));
    assertEquals(mapper.writeValueAsString(item), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_404_when_item_does_not_exist() throws Exception {
    // Arrange
    when(menuItemRepository.findById(eq(999L))).thenReturn(Optional.empty());

    // Act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems").param("id", "999"))
            .andExpect(status().isNotFound())
            .andReturn();

    // Assert
    verify(menuItemRepository, times(1)).findById(eq(999L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 999 not found", json.get("message"));
  }

  // PUT tests — Issue #11

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/ucsbdiningcommonsmenuitems")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/ucsbdiningcommonsmenuitems")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update_existing_item() throws Exception {
    UCSBDiningCommonsMenuItem original =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("carrillo")
            .name("Chicken Salad")
            .station("Entrees")
            .build();

    UCSBDiningCommonsMenuItem updated =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Grilled Chicken Salad")
            .station("Salad Bar")
            .build();

    when(menuItemRepository.findById(eq(1L))).thenReturn(Optional.of(original));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitems")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(updated))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemRepository, times(1)).findById(1L);
    verify(menuItemRepository, times(1)).save(updated);

    assertEquals(mapper.writeValueAsString(updated), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_gets_404_when_updating_nonexistent_item() throws Exception {
    UCSBDiningCommonsMenuItem incoming =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Grilled Chicken Salad")
            .station("Entrees")
            .build();

    when(menuItemRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitems")
                    .param("id", "999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incoming))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemRepository, times(1)).findById(999L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 999 not found", json.get("message"));
  }

  // DELETE tests — Issue #12

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/ucsbdiningcommonsmenuitems").param("id", "1").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/ucsbdiningcommonsmenuitems").param("id", "1").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_existing_item() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Chicken Salad")
            .station("Entrees")
            .build();

    when(menuItemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitems").param("id", "1").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemRepository, times(1)).findById(1L);
    verify(menuItemRepository, times(1)).delete(item);

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 1 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_gets_404_when_deleting_nonexistent_item() throws Exception {
    when(menuItemRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitems").param("id", "999").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemRepository, times(1)).findById(999L);
    verify(menuItemRepository, times(0))
        .delete(org.mockito.ArgumentMatchers.any(UCSBDiningCommonsMenuItem.class));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 999 not found", json.get("message"));
  }
}
