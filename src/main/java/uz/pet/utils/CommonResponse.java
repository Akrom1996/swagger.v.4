package uz.pet.utils;

/********************************************
 *   @author Bazarbayev_Mansurjon
 *   @date 15.01.2020
 *   @project VisaSubscriberService
 *   @package uz.hamkor.visa.responses
 ********************************************/
public class CommonResponse {
    String httpStatus;
    String errorCode;
    String errorMessage;
    String errorType;
    Object response;

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }


}
