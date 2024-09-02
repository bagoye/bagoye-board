package com.bagoye.board.common;

public interface ResponseCode {

    // Http status 200
    String SUCCESS = "SU";

    // Http status 400
    String VALIDATION_FAILED = "VF";
    String DUPLICATE_EMAIL = "DE";
    String DUPLICATE_NICKNAME = "DN";
    String DUPLICATE_TEL_NUMBER = "DT";
    String NOT_EXITED_USER = "NU";
    String NOT_EXITED_BOARD = "NB";

    // Http status 401
    String SIGN_IN_FAIL = "SF";
    String AUTHORIZATION_FAIL = "AF";

    // Http status 403
    String NO_PERMISSION = "NP";

    // Http status 500
    String DATABASE_ERROR = "DBE";


}
