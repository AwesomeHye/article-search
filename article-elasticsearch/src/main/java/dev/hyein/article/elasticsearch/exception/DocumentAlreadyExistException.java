package dev.hyein.article.elasticsearch.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 도큐먼트 ID 존재 시 발생하는 Exception
 */
@Getter @Setter
public class DocumentAlreadyExistException extends RuntimeException{

    public DocumentAlreadyExistException(String docId, String index) {
        super(String.format("Doc id [%s] is already exist in [%s] index.", docId, index));
    }
}
