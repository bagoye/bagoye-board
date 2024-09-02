enum ResponseCode {
    SUCCESS = "SU",

    // Http status 400
    VALIDATION_FAILED = "VF",
    DUPLICATE_EMAIL = "DE",
    DUPLICATE_NICKNAME = "DN",
    DUPLICATE_TEL_NUMBER = "DT",
    NOT_EXITED_USER = "NU",
    NOT_EXITED_BOARD = "NB",

    // Http status 401
    SIGN_IN_FAIL = "SF",
    AUTHORIZATION_FAIL = "AF",

    // Http status 403
    NO_PERMISSION = "NP",

    // Http status 500
    DATABASE_ERROR = "DBE"
}

export default ResponseCode;