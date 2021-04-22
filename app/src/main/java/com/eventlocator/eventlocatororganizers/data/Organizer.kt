package com.eventlocator.eventlocatororganizers.data

class Organizer (var id: Long, var name: String, var email: String, var about: String,
                 var rating: Double,  var socialMediaAccounts: ArrayList<SocialMediaAccount>,
                 var image: String, var numberOfFollowers: Int, var phoneNumber: String, var password: String, var status: Int) {

    //status has no use as of now but it may be needed in the future
    private constructor(organizerBuilder: OrganizerBuilder): this(-1, organizerBuilder.name, organizerBuilder.email, organizerBuilder.about,
                 -1.0, organizerBuilder.socialMediaAccounts, "", -1, organizerBuilder.phoneNumber,
                        organizerBuilder.password, 0)

    class OrganizerBuilder (var name: String, var email: String, var about:String, var phoneNumber: String, var password: String) {

        lateinit var socialMediaAccounts: ArrayList<SocialMediaAccount>

        fun setSocialMediaAccounts(socialMediaAccounts: ArrayList<SocialMediaAccount>): OrganizerBuilder{
            this.socialMediaAccounts = socialMediaAccounts
            return this
        }

        fun build(): Organizer{
            return Organizer(this)
        }
    }

}