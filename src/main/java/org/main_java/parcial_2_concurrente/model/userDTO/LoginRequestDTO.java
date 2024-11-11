package org.main_java.parcial_2_concurrente.model.userDTO;

import lombok.*;


@Data
@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDTO {
    private String correo;
    private String password;
}