export default (values) => {
  const errors = {}
  const { currentPassword, confirmPassword, password } = values
  if (!currentPassword) {
    errors.currentPassword = 'Current password must not be empty'
  }
  if (typeof password === 'undefined' || password === '') {
    errors.password = 'Password must not be empty'
  }
  if (password && password !== confirmPassword) {
    errors.confirmPassword = 'Password does not match'
  }
  return errors
}
