
let validateProp = {
  type: [Array, Object],
  default: undefined
}

export { validateProp };

export default {
  props: {
    validate: validateProp, 
  },
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
        })
      }
      if(this.validate) {
        return [...validations, ...Array.of(this.validate)];
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
        isValid: false,
        skipped: false,
        message: ''
      }
      //Go through validation options
      this.validations.forEach(validationInput => {
        let validationType = validationInput instanceof String || typeof validationInput === 'string'
          ? validationInput : validationInput.type;
        
        let doValidate = validationInput.active != undefined ? validationInput.active : this.hadInput;

        //Return if we are not validating yet
        if(!doValidate) {
          status.skipped = true;
          return;
        }
        status.skipped = false;

        switch(validationType) {
          case 'required':
            if(this.type == "checkbox" && this.value == false) {
              status.message = validationInput.message ? validationInput.message : "This checkbox is required to be checked";
              status.isValid = false;
            } else if (this.value == undefined || this.value.trim() == "") {
              status.message = validationInput.message ? validationInput.message : "This field is required to be filled out";
              status.isValid = false;
            } else {
              status.isValid = true;
            }
            break;
          case 'custom':
            if(!(validationInput.validationFn())) {
              //Not valid
              status.message = validationInput.message; 
              status.isValid = false;
            } else {
              //Is valid
              status.isValid = true;
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
    };
  },
};
