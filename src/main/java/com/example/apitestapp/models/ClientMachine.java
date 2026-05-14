package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMachine {

    private String id;
    private String userId;
    private String machineName;
    private String os;
    private String ipAddress;
    private String hostname;

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
}
