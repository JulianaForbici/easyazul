package easy.azul.api.infra.validation.cpf;

import br.com.caelum.stella.validation.CPFValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidador implements ConstraintValidator<CPF, String> {

    private final CPFValidator validator = new CPFValidator();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        return validator.invalidMessagesFor(value).isEmpty();
    }
}