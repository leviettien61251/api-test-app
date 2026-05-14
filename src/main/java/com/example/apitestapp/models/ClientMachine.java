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
    private String osName;         // Windows, macOS, Linux
    private String osVersion;      // 10 Pro 22H2, 14.3 Sonoma
    private String ipAddress;      // IPv4 hoặc IPv6
    private String cpuInfo;        // Intel Core i7-13700K
    private String ramInfo;        // 32GB DDR5
    private String hostname;

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
}
