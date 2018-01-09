export default function validate(fields) {
  const errors = {}
  const { username, password } = fields
  if (typeof username === 'undefined' || username === '') {
    errors.username = 'Username must not be empty'
  }
  if (typeof password === 'undefined' || password === '') {
    errors.password = 'Password must not be empty'
  }
  return errors
}
