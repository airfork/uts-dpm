// User list data
export const LIST_EMAIL_MESSAGE =
  'This will send a points balance email to all users.';
export const LIST_RESET_MESSAGE =
  'This will reset the points value for all part-time drivers.';

export type ListOutputKey = 'email' | 'reset';

// User details data
export const DETAIL_DELETE_MESSAGE = 'This will permanently delete this user.';
export const DETAIL_EMAIL_MESSAGE =
  'This will send a points balance email to this user.';
export const DETAIL_RESET_MESSAGE = "This will reset this user's password.";

export type DetailOutputKey = 'delete' | 'email' | 'reset';
