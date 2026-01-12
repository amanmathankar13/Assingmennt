package com.factory.events.dto;

import java.util.ArrayList;
import java.util.List;

import com.factory.events.payload.Rejection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponse {

    private int accepted;
    private int updated;
    private int deduped;
    private int rejected;

    private List<Rejection> rejections = new ArrayList<>();
}
