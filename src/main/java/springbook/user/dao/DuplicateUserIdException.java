package springbook.user.dao;

public class DuplicateUserIdException extends Exception{
    public DuplicateUserIdException(Throwable cause) {
        super(cause);
    }
}
