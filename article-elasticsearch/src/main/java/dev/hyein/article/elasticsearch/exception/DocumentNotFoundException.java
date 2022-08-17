package dev.hyein.article.elasticsearch.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 도큐먼트 ID 미존재 시 발생하는 Exception
 */
@Getter @Setter
public class DocumentNotFoundException extends RuntimeException{

    public DocumentNotFoundException(String docId, String index) {
        super(String.format("Doc id [%s] is not exist in [%s] index.", docId, index));
    }
}
