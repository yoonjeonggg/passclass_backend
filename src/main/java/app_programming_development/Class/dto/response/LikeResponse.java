package app_programming_development.Class.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LikeResponse {
    @JsonProperty("isLiked")
    private boolean isLiked;
}
