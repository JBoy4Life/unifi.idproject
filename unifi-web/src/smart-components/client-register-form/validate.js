export default function validate(fields) {
  const errors = {}
  const { displayName, logo } = fields
  if (typeof displayName === 'undefined' || displayName === '') {
    errors.displayName = 'displayName must not be empty'
  }

  if (typeof logo === 'undefined' || logo === '') {
    errors.logo = 'logo must not be empty'
  }
  return errors
}
