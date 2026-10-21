package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Provides endpoints for menu item reviews. */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * Get a single menu item review by its ID.
   *
   * @param id the review ID
   * @return the matching review
   * @throws EntityNotFoundException if the review does not exist
   */
  @Operation(
      summary = "Get a single menu item review",
      description = "Returns the review with the given ID, or 404 if it does not exist.")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(
      @Parameter(name = "id", description = "Review ID, not the menu item ID") @RequestParam
          Long id) {

    return menuItemReviewRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));
  }

  /**
   * List all menu item reviews.
   *
   * @return all menu item reviews
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    return menuItemReviewRepository.findAll();
  }

  /**
   * Create a menu item review.
   *
   * @param itemId the ID of the menu item being reviewed
   * @param reviewerEmail the reviewer's email
   * @param stars the rating from 0 to 5
   * @param dateReviewed the date and time of the review
   * @param comments the review text
   * @return the saved review
   */
  @Operation(summary = "Create a menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam Long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(
              name = "dateReviewed",
              description = "Review date and time, for example 2026-10-20T18:00:00")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments) {

    MenuItemReview review = new MenuItemReview();
    review.setItemId(itemId);
    review.setReviewerEmail(reviewerEmail);
    review.setStars(stars);
    review.setDateReviewed(dateReviewed);
    review.setComments(comments);

    return menuItemReviewRepository.save(review);
  }

  /**
   * Delete a menu item review.
   *
   * @param id the ID of the review to delete
   * @return a message confirming the deletion
   * @throws EntityNotFoundException if the review does not exist
   */
  @Operation(
      summary = "Delete a menu item review",
      description =
          "Deletes the review identified by the query parameter ID, not the menu item ID. For"
              + " example, id=123 returns 200 with message 'record 123 deleted', or 404 with"
              + " message 'record 123 not found' if the review does not exist. Requires an admin"
              + " login.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteMenuItemReview(
      @Parameter(name = "id", description = "ID of the review to delete") @RequestParam Long id) {
    MenuItemReview review =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("record %s not found".formatted(id)));

    menuItemReviewRepository.delete(review);
    return genericMessage("record %s deleted".formatted(id));
  }

  /**
   * Update an existing menu item review.
   *
   * @param id the ID of the review to update
   * @param incoming the replacement review fields
   * @return the updated review
   * @throws EntityNotFoundException if the review does not exist
   */
  @Operation(
      summary = "Update a menu item review",
      description =
          "Updates itemId, reviewerEmail, stars, dateReviewed, and comments while preserving "
              + "the stored review ID. Uses the query parameter ID and ignores any ID in the "
              + "JSON body. Returns 404 if the review does not exist. Requires an admin login.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id", description = "ID of the review to update") @RequestParam Long id,
      @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview review =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    review.setItemId(incoming.getItemId());
    review.setReviewerEmail(incoming.getReviewerEmail());
    review.setStars(incoming.getStars());
    review.setDateReviewed(incoming.getDateReviewed());
    review.setComments(incoming.getComments());

    menuItemReviewRepository.save(review);

    return review;
  }
}
