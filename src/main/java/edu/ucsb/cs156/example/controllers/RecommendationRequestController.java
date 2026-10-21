package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequests */
@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all recommendation requests
   *
   * @return an iterable of RecommendationRequest
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    return recommendationRequestRepository.findAll();
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail email of the student requesting the letter
   * @param professorEmail email of the professor asked to write it
   * @param explanation what the letter is for
   * @param dateRequested when the request was made
   * @param dateNeeded when the letter is needed
   * @param done whether the request has been fulfilled
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(
              name = "dateRequested",
              description = "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS)")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(
              name = "dateNeeded",
              description = "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS)")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail(requesterEmail)
            .professorEmail(professorEmail)
            .explanation(explanation)
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(done)
            .build();

    return recommendationRequestRepository.save(recommendationRequest);
  }
}
