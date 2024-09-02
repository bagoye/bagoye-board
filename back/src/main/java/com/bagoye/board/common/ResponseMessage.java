package com.bagoye.board.common;

public interface ResponseMessage {
    // Http status 200
    String SUCCESS = "Success.";

    // Http status 400
    String VALIDATION_FAILED = "Validation Failed.";
    String DUPLICATE_EMAIL = "Duplicate Email.";
    String DUPLICATE_NICKNAME = "Duplicate Nickname.";
    String DUPLICATE_TEL_NUMBER = "Duplicate Tel number.";
    String NOT_EXITED_USER = "This user does not exist.";
    String NOT_EXITED_BOARD = "This board does not exist.";

    // Http status 401
    String SIGN_IN_FAIL = "Login information mismatch.";
    String AUTHORIZATION_FAIL = "Authorization Failed.";

    // Http status 403
    String NO_PERMISSION = "Do not have permission.";

    // Http status 500
    String DATABASE_ERROR = "Database Error.";
}
