package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isForbidden());
    verifyNoInteractions(menuItemReviewRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_an_empty_list() throws Exception {
    when(menuItemReviewRepository.findAll()).thenReturn(List.of());

    MvcResult response =
        mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();
    assertEquals("[]", response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all_reviews() throws Exception {
    MenuItemReview first =
        MenuItemReview.builder()
            .id(1L)
            .itemId(7L)
            .reviewerEmail("first@example.com")
            .stars(5)
            .dateReviewed(LocalDateTime.parse("2026-10-20T18:00:00"))
            .comments("Enjoyed this menu item")
            .build();
    MenuItemReview second =
        MenuItemReview.builder()
            .id(2L)
            .itemId(7L)
            .reviewerEmail("second@example.com")
            .stars(0)
            .dateReviewed(LocalDateTime.parse("2026-10-20T19:00:00"))
            .comments("Would choose something else")
            .build();
    List<MenuItemReview> expected = List.of(first, second);
    when(menuItemReviewRepository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();
    assertEquals(mapper.writeValueAsString(expected), response.getResponse().getContentAsString());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(reviewPost().with(csrf())).andExpect(status().isForbidden());
    verifyNoInteractions(menuItemReviewRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc.perform(reviewPost().with(csrf())).andExpect(status().isForbidden());
    verifyNoInteractions(menuItemReviewRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_cannot_post_without_csrf() throws Exception {
    mockMvc.perform(reviewPost()).andExpect(status().isForbidden());
    verifyNoInteractions(menuItemReviewRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_can_post_a_review_and_receive_the_saved_record() throws Exception {
    LocalDateTime dateReviewed = LocalDateTime.parse("2026-10-20T18:00:00");
    MenuItemReview incoming =
        MenuItemReview.builder()
            .itemId(7L)
            .reviewerEmail("reviewer@example.com")
            .stars(4)
            .dateReviewed(dateReviewed)
            .comments("Fresh and tasty")
            .build();
    MenuItemReview saved =
        MenuItemReview.builder()
            .id(42L)
            .itemId(7L)
            .reviewerEmail("reviewer@example.com")
            .stars(4)
            .dateReviewed(dateReviewed)
            .comments("Fresh and tasty")
            .build();
    when(menuItemReviewRepository.save(eq(incoming))).thenReturn(saved);

    MvcResult response =
        mockMvc.perform(reviewPost().with(csrf())).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).save(eq(incoming));
    assertEquals(mapper.writeValueAsString(saved), response.getResponse().getContentAsString());
  }

  private MockHttpServletRequestBuilder reviewPost() {
    return post("/api/MenuItemReview/post")
        .param("itemId", "7")
        .param("reviewerEmail", "reviewer@example.com")
        .param("stars", "4")
        .param("dateReviewed", "2026-10-20T18:00:00")
        .param("comments", "Fresh and tasty");
  }
}
