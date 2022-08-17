package dev.hyein.article.app.utils;

public class ErrorHandlingUtils {
    /**
     * ErrorStackTrace 출력
     * @param e
     * @param limit stack 상위 개수
     * @return
     */
    public static String getErrorStackTrace(Exception e, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage() + System.lineSeparator());
        for (int i = 0; i < limit; i++) {
            StackTraceElement stackTraceElement = e.getStackTrace()[i];
            sb.append(String.format("%s %s.%s:%s",stackTraceElement.getFileName(), stackTraceElement.getClassName(),
                    stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()) + System.lineSeparator());
        }
        return sb.toString();
    }
}
