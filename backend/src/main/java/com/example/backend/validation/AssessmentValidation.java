package com.example.backend.validation;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;

@Component
public class AssessmentValidation {
    public void validate(String type, String target) {
        if (!type.equals("SERVER") && !type.equals("WEB")) {
            throw new IllegalArgumentException(
                    "Type phải là SERVER hoặc WEB"
            );
        }

        if (type.equals("SERVER")) {
            validateIpOrCidr(target);
        }

        if (type.equals("WEB")) {
            validateUrl(target);
        }
    }

    private void validateIpOrCidr(String target) {

        if (target.contains("/")) {
            String[] parts = target.split("/");

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "CIDR không hợp lệ"
                );
            }

            validateIp(parts[0]);

            try {
                int prefix = Integer.parseInt(parts[1]);

                if (prefix < 0 || prefix > 32) {
                    throw new IllegalArgumentException(
                            "CIDR prefix phải từ 0 đến 32"
                    );
                }

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "CIDR prefix không hợp lệ"
                );
            }

            return;
        }

        validateIp(target);
    }

    private void validateIp(String target) {

        try {
            InetAddress.getByName(target);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Địa chỉ IP không hợp lệ"
            );
        }
    }

    private void validateUrl(String target) {

        try {
            URI uri = URI.create(target);

            String scheme = uri.getScheme();

            if (scheme == null ||
                    (!scheme.equalsIgnoreCase("http")
                            && !scheme.equalsIgnoreCase("https"))) {

                throw new IllegalArgumentException(
                        "URL phải sử dụng http hoặc https"
                );
            }

            if (uri.getHost() == null) {
                throw new IllegalArgumentException(
                        "URL không hợp lệ"
                );
            }

        } catch (Exception e) {

            if (e instanceof IllegalArgumentException) {
                throw e;
            }

            throw new IllegalArgumentException(
                    "URL không hợp lệ"
            );
        }
    }
}
