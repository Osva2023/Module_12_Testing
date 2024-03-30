package com.rocketFoodDelivery.rocketFood.util;

import org.springframework.http.ResponseEntity;

import com.rocketFoodDelivery.rocketFood.dtos.ApiErrorDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiResponseDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;

import org.springframework.http.HttpStatus;

/**
 * Custom utility class for handling API responses.
 */

public class ResponseBuilder {

    /* Success responses */

    public static ResponseEntity<Object> buildOkResponse(Object data) {
        return createSuccessResponse("Success", data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> buildCreatedResponse(Object data) {
        return createSuccessResponse("Success", data, HttpStatus.CREATED);
    }

    private static ResponseEntity<Object> createSuccessResponse(String message, Object data, HttpStatus status) {
        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage(message);
        response.setData(data);
        return new ResponseEntity<>(response, status);
    }

    /* Error responses */

    public static ResponseEntity<Object> buildResourceNotFoundExceptionResponse(ResourceNotFoundException ex) {
        return createErrorResponse("404 Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    public static ResponseEntity<Object> buildBadRequestExceptionResponse(BadRequestException ex) {
        return createErrorResponse("400 Bad Request", ex.getDetails(), HttpStatus.BAD_REQUEST);
    }

    // public static ResponseEntity<Object> buildInternalServerErrorExceptionResponse(InternalServerErrorException ex) {
    //     return createErrorResponse("500 Internal Server Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    // }

    private static ResponseEntity<Object> createErrorResponse(String error, String details, HttpStatus status) {
        ApiErrorDTO response = new ApiErrorDTO();
        response.setError(error);
        response.setDetails(details);
        return new ResponseEntity<>(response, status);
    }
}