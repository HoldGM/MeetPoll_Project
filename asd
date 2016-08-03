    public void resetPassword(View v) {
        resetPasswordFragment reset = new resetPasswordFragment();
        reset.show(getFragmentManager(),"resetPassword");
    }
    public static class resetPasswordFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText userInput = new EditText(getActivity());
            userInput.setHint("Enter email");
            builder.setView(userInput);
            builder.setMessage("Send reset email to ")
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String email = userInput.getText().toString();
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
//                                                Toast.makeText(getView().getContext(), "Email sent to " + email + ".", Toast.LENGTH_LONG).show();
                                                t.setText("Email sent to " + email + ".");
                                                t.show();
                                            } else {
//                                                Toast.makeText(getView().getContext(), "Email failed to send to " + email + ".", Toast.LENGTH_LONG).show();
                                                t.setText("Email failed to send to " + email + ".");
                                                t.show();
                                            }
                                        }
                                    });
                            dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }
    
    <?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
<LinearLayout
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    tools:context="odb234.meetpoll.LoginActivity"
    android:orientation="vertical">

    <EditText
        android:id="@+id/login_username"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter email"
        android:backgroundTint="@color/splash_orange"
        android:padding="10dp"/>
    <EditText
        android:id="@+id/login_password"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter Password"
        android:backgroundTint="@color/splash_orange"
        android:padding="10dp"
        android:layout_marginTop="@dimen/activity_horizontal_margin"
        android:password="true"/>
    <CheckBox
        android:id="@+id/remember_check"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Remember me"/>
    <Button
        android:id="@+id/login_login"
        android:layout_width="125dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:background="@drawable/edit_text_bg"
        android:text="Login"
        android:textColor="@android:color/white"
        android:textSize="@dimen/event_text_size"
        android:onClick="login"
        android:layout_centerInParent="true"/>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="or"
        android:layout_gravity="center_horizontal"
        android:textSize="@dimen/event_text_size"
        android:layout_marginTop="@dimen/activity_vertical_margin"/>
    <Button
        android:id="@+id/login_register"
        android:layout_width="125dp"
        android:layout_height="wrap_content"
        android:text="Register"
        android:textSize="@dimen/event_text_size"
        android:background="@drawable/edit_text_bg"
        android:layout_gravity="center_horizontal"
        android:textColor="@android:color/white"
        android:layout_marginTop="@dimen/activity_vertical_margin"
        android:onClick="register"/>
</LinearLayout>
    <TextView
        android:id="@+id/login_forgot_password"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Forgot password?"
        android:textSize="@dimen/event_text_size"
        android:layout_alignParentBottom="true"
        android:clickable="true"
        android:onClick="resetPassword"
        android:layout_marginLeft="@dimen/activity_horizontal_margin"
        android:layout_marginBottom="@dimen/activity_vertical_margin"/>

</RelativeLayout>
