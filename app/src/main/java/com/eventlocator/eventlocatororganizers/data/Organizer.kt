package com.eventlocator.eventlocatororganizers.data

class Organizer (var id: Long, var name: String, var email: String, var about/*description*/: String,
                 var rating: Double, var followers: List<Participant>, var socialMediaAccounts: List<SocialMediaAccount>,
                 var upcomingEvents: List<Event>, var previousEvents: List<Event>, var canceledEvents: List<Event>) {


    private constructor(organizerBuilder: OrganizerBuilder): this(-1, organizerBuilder.name, organizerBuilder.email, organizerBuilder.about,
                 -1.0, ArrayList<Participant>(), organizerBuilder.socialMediaAccounts,
                        ArrayList<Event>(), ArrayList<Event>(), ArrayList<Event>())

    class OrganizerBuilder (var name: String, var email: String, var about:String, var phoneNumber: String, var password: String) {

        lateinit var socialMediaAccounts: List<SocialMediaAccount>

        fun setSocialMediaAccounts(socialMediaAccounts: List<SocialMediaAccount>): OrganizerBuilder{
            this.socialMediaAccounts = socialMediaAccounts
            return this
        }

        fun build(): Organizer{
            return Organizer(this)
        }
    }

}