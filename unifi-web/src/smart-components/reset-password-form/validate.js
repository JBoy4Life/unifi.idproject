export default (values) => {
  const errors = {}
  const { confirmPassword, password } = values
  if (typeof password === 'undefined' || password === '') {
    errors.password = 'Password must not be empty'
  }
  if (password && password !== confirmPassword) {
    errors.confirmPassword = 'Password does not match'
  }
  return errors
}
