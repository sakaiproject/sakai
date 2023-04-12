import i18nMixn from "../mixins/i18n-mixn.js";

let validateProp = {
  type: [Array, Object],
  default: null
};

export { validateProp };

export default {
  props: {
    validate: validateProp, 
  },
  mixins: [i18nMixn],
  computed: {
    validations() {
      let validations = [];
      if(this.required) {
        validations.push({ type: "required" });
      }
      if(this.maxlength) {
        validations.push({
          type: "maxlength",
          value: this.maxlength
        });
      }
      if(this.minlength) {
        validations.push({
          type: "minlength",
          value: this.minlength
        });
      }
      if(this.validate) {
        if(Array.isArray(this.validate)){
          return [...validations, ...this.validate];
        } else {
          return [...validations, ...Array.of(this.validate)];
        }
      } else {
        return validations;
      }
    },
    hasValidation() {
      return this.validations.length > 0;
    },
    validationStatus() {
      //Setup Status boject
      var status = {
        isValid: true,
        skipped: false,
        message: ''
      };
      //Go through validation options
      this.validations.forEach((validationInput) => {
        let validationType = validationInput instanceof String || typeof validationInput === 'string'
          ? validationInput : validationInput.type;
        
        let doValidate = validationInput?.active || this.hadInput;

        //Return if we are not validating yet
        if(!doValidate) {
          status.skipped = true;
          return;
        }
        status.skipped = false;
        switch(validationType) {
          case 'required':
            if(this.type === "checkbox" && this.value === false) {
              status.message = validationInput.message ? validationInput.message : this.i18n?.empty_checkbox;
              status.isValid = false;
            } else if (!this.value || this.value.trim() === "") {
              status.message = validationInput.message ? validationInput.message : this.i18n?.empty_field;
              status.isValid = false;
            } else {
              status.isValid = status.isValid && true;
            }
            break;
          case 'maxlength':
            if (this.value && this.value.length > validationInput.value) {
              status.message = validationInput.message ? validationInput.message : this.i18n?.maxlenght_field;
              status.isValid = false;
            } else {
              status.isValid = status.isValid && true;
            }
            break;
          case 'custom':
            if(!(validationInput.validationFn())) {
              //Not valid
              status.message = validationInput.message; 
              status.isValid = false;
            } else {
              //Is valid
              status.isValid = status.isValid && true;
            }
            break;
          default:
            console.error(`Unknown validation type '${validationType}'`);
            break;
        }
      });
      return status;
    }
  },
  watch: {
    validationStatus(newStatus) {
      this.$emit('validation', { ...newStatus }.isValid );
    }
  },
  data() {
    return { 
      hadInput: false,
      i18nProps: "validations"
    };
  },
};
