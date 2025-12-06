package main;

public class SelfException extends Exception
{
    SelfException(String message)
    {
        super(message);
    }
}
