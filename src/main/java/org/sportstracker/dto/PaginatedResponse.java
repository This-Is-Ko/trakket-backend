package org.sportstracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse {

    int pageNumber;
    int pageSize;
    long totalElements;
    boolean last;

}
