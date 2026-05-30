package easy.azul.api.infra.validation.cnpj;

import br.com.caelum.stella.validation.CNPJValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNPJValidador implements ConstraintValidator<CNPJ, String> {

    private final CNPJValidator validator = new CNPJValidator();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return validator.invalidMessagesFor(value).isEmpty();
    }
}