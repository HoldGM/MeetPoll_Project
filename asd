class sendInvites extends AsyncTask<String, Contact, Void> {
        @Override
        protected Void doInBackground(String... path) {
            for(int i=0;i<invitees.size();i++)
            {
                final int finI = i;
                final String finPath = path[0];
                mRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()) {
                            if(child.child("phone").getValue().toString().equals(invitees.get(finI).getPhone()) && !child.child("phone").getValue().toString().equals(sp.getString("phone","")))
                            {
                                //add event to their invited list
                                child.child("invited-events").getRef().push().setValue(finPath);
                                break;
                            }
                            else
                            {
                                //send sms message
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            startActivity(new Intent(ContactsListActivity.this, MainActivity.class));
        }
    }
