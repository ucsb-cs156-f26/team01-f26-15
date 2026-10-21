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
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;

  @MockitoBean UserRepository userRepository;

  // Test 1: Logged-out users cannot GET all articles

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(403));
  }

  // Test 2: Logged-in users can GET all articles

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200));
  }

  // Test 3: Logged-out users cannot POST

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Spring Boot Guide")
                .param("url", "https://spring.io")
                .param("explanation", "Helpful Spring Boot reference")
                .param("email", "student@ucsb.edu")
                .param("dateAdded", "2026-10-20T14:30:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Test 4: Regular users cannot POST

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Spring Boot Guide")
                .param("url", "https://spring.io")
                .param("explanation", "Helpful Spring Boot reference")
                .param("email", "student@ucsb.edu")
                .param("dateAdded", "2026-10-20T14:30:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Test 5: GET all returns the correct articles

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    LocalDateTime date1 = LocalDateTime.parse("2026-10-20T14:30:00");
    LocalDateTime date2 = LocalDateTime.parse("2026-10-21T10:00:00");

    Articles article1 =
        Articles.builder()
            .id(1L)
            .title("Spring Boot Guide")
            .url("https://spring.io")
            .explanation("Helpful Spring Boot reference")
            .email("student@ucsb.edu")
            .dateAdded(date1)
            .build();

    Articles article2 =
        Articles.builder()
            .id(2L)
            .title("React Testing Guide")
            .url("https://react.dev")
            .explanation("Helpful React reference")
            .email("student2@ucsb.edu")
            .dateAdded(date2)
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>();
    expectedArticles.addAll(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  // Test 6: Admin can POST a new article

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {

    LocalDateTime date = LocalDateTime.parse("2026-10-20T14:30:00");

    Articles article =
        Articles.builder()
            .title("Spring Boot Guide")
            .url("https://spring.io")
            .explanation("Helpful Spring Boot reference")
            .email("student@ucsb.edu")
            .dateAdded(date)
            .build();

    when(articlesRepository.save(eq(article))).thenReturn(article);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "Spring Boot Guide")
                    .param("url", "https://spring.io")
                    .param("explanation", "Helpful Spring Boot reference")
                    .param("email", "student@ucsb.edu")
                    .param("dateAdded", "2026-10-20T14:30:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).save(article);

    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/articles").param("id", "7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_id_exists() throws Exception {

    LocalDateTime date = LocalDateTime.parse("2026-10-20T14:30:00");

    Articles article =
        Articles.builder()
            .id(7L)
            .title("Spring Boot Guide")
            .url("https://spring.io")
            .explanation("Helpful Spring Boot reference")
            .email("student@ucsb.edu")
            .dateAdded(date)
            .build();

    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(eq(7L));

    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_404_when_id_does_not_exist() throws Exception {

    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "123"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(eq(123L));

    Map<String, Object> json = responseToJson(response);

    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 123 not found", json.get("message"));
  }

  // Test 10: Logged-out users cannot PUT

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    Articles incoming =
        Articles.builder()
            .title("Updated Article")
            .url("https://example.com")
            .explanation("Updated explanation")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T10:00:00"))
            .build();

    mockMvc
        .perform(
            put("/api/articles")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Test 11: Regular users cannot PUT

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    Articles incoming =
        Articles.builder()
            .title("Updated Article")
            .url("https://example.com")
            .explanation("Updated explanation")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T10:00:00"))
            .build();

    mockMvc
        .perform(
            put("/api/articles")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Test 12: Admin can edit an existing article

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {

    Articles original =
        Articles.builder()
            .id(1L)
            .title("Original Title")
            .url("https://old.example.com")
            .explanation("Original explanation")
            .email("old@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-20T14:30:00"))
            .build();

    Articles edited =
        Articles.builder()
            .id(1L)
            .title("Updated Title")
            .url("https://new.example.com")
            .explanation("Updated explanation")
            .email("new@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T10:00:00"))
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(articlesRepository.findById(eq(1L))).thenReturn(Optional.of(original));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(1L);
    verify(articlesRepository, times(1)).save(edited);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  // Test 13: Admin cannot edit a nonexistent article

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_article_that_does_not_exist() throws Exception {

    Articles incoming =
        Articles.builder()
            .title("Updated Title")
            .url("https://new.example.com")
            .explanation("Updated explanation")
            .email("new@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T10:00:00"))
            .build();

    String requestBody = mapper.writeValueAsString(incoming);

    when(articlesRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(999L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 999 not found", json.get("message"));
  }

  // Test 14: Logged-out users cannot DELETE

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/articles").param("id", "1").with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Test 15: Regular users cannot DELETE

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/articles").param("id", "1").with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Test 16: Admin can delete an existing article

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_article() throws Exception {

    Articles article =
        Articles.builder()
            .id(1L)
            .title("Spring Boot Guide")
            .url("https://spring.io")
            .explanation("Helpful Spring Boot reference")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-20T14:30:00"))
            .build();

    when(articlesRepository.findById(eq(1L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(delete("/api/articles").param("id", "1").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(1L);
    verify(articlesRepository, times(1)).delete(article);

    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 1 deleted", json.get("message"));
  }

  // Test 17: Admin cannot delete a nonexistent article

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_article_that_does_not_exist() throws Exception {

    when(articlesRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/articles").param("id", "999").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(999L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 999 not found", json.get("message"));
  }
}
