package com.evostackr.smscampaignmanager.util

import com.evostackr.smscampaignmanager.data.local.dao.TemplateDao
import com.evostackr.smscampaignmanager.data.local.entity.TemplateEntity

object TemplateSeeder {

    suspend fun seedBuiltInTemplates(templateDao: TemplateDao) {
        if (templateDao.getBuiltInTemplateCount() > 0) {
            return // Already seeded
        }

        val templates = mutableListOf<TemplateEntity>()

        // Helper function
        fun add(title: String, content: String, category: String) {
            templates.add(
                TemplateEntity(
                    title = title,
                    content = content,
                    category = category,
                    isBuiltIn = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        // -- Marketing --
        add("Flash Sale Alert", "Hi {name}! ⚡ Our 24-hour Flash Sale is LIVE. Get up to 50% off storewide. Shop now: {custom} Reply STOP to opt out.", "Marketing")
        add("New Arrival", "Hey {name}, fresh styles just dropped at {company}! Be the first to check out our new collection. Shop here: {custom}", "Marketing")
        add("Exclusive Discount", "{name}, enjoy 20% off your next purchase at {company} with code SAVE20. Valid until {date}. Visit {custom} today!", "Marketing")
        add("Abandoned Cart", "Hi {name}, you left something behind in your cart! Complete your order now and get 10% off. Link: {custom}", "Marketing")
        add("Weekend Special", "Happy Weekend {name}! Celebrate with buy 1 get 1 free on selected items at {company}. See deals: {custom}", "Marketing")
        add("VIP Access", "Hey VIP! {name}, you have early access to our massive clearance sale. Starts tomorrow at {time}. Check it out: {custom}", "Marketing")
        
        // -- Business --
        add("Office Closure", "Dear {name}, please note that {company} will be closed on {date} for a company holiday. Normal hours resume the following day.", "Business")
        add("Service Maintenance", "Hi {name}. We will be performing scheduled maintenance on {date} at {time}. Some services may be temporarily unavailable.", "Business")
        add("Policy Update", "Dear {name}, we have updated our privacy policy at {company}. Please review the changes here: {custom}", "Business")
        add("Feedback Request", "Hi {name}! Thanks for choosing {company}. We'd love to hear your feedback. Please take a 1-minute survey: {custom}", "Business")
        add("Subscription Renewal", "Hello {name}, your subscription with {company} will automatically renew on {date}. Total amount: {amount}. Thank you!", "Business")
        add("Meeting Confirmation", "Hi {name}, this confirms our meeting on {date} at {time}. Location/Link: {custom}. See you soon! - {company}", "Business")

        // -- Payment Reminder --
        add("Invoice Due Soon", "Hi {name}, a quick reminder that invoice #{custom} for {amount} is due on {date}. Pay online here: {custom}", "Payment Reminder")
        add("Payment Overdue", "URGENT: {name}, your payment of {amount} to {company} is past due. Please submit payment immediately to avoid late fees. {custom}", "Payment Reminder")
        add("Payment Received", "Thank you {name}! We have received your payment of {amount} on {date}. Receipt: {custom}. - {company}", "Payment Reminder")
        add("Upcoming Auto-Pay", "Hi {name}. Your auto-pay of {amount} is scheduled for {date}. Ensure sufficient funds are available. - {company}", "Payment Reminder")
        add("Failed Payment", "Dear {name}, we couldn't process your payment of {amount} for {company}. Please update your billing info here: {custom}", "Payment Reminder")

        // -- Appointment Reminder --
        add("Upcoming Appointment", "Hi {name}, reminder for your appointment at {company} on {date} at {time}. Reply YES to confirm or CALL to reschedule.", "Appointment Reminder")
        add("Missed Appointment", "Hello {name}, you missed your appointment with {company} today at {time}. Please call us at {phone} to reschedule.", "Appointment Reminder")
        add("Reschedule Confirmation", "Hi {name}, your appointment has been successfully rescheduled to {date} at {time}. See you then! - {company}", "Appointment Reminder")
        add("Check-in Reminder", "Hi {name}, please complete your pre-check-in form before your appointment on {date}. Form link: {custom}", "Appointment Reminder")
        add("Follow-up Booking", "Hi {name}, it's time for your regular check-up! Call us at {phone} or book online at {custom}. - {company}", "Appointment Reminder")

        // -- Greetings --
        add("Welcome Message", "Welcome to {company}, {name}! We're thrilled to have you. If you have any questions, just reply to this text.", "Greetings")
        add("Happy Birthday", "Happy Birthday {name}! 🎉 Wishing you a fantastic day. Enjoy a special gift from {company}: {custom}", "Greetings")
        add("Anniversary", "Happy Anniversary {name}! You've been with {company} for another great year. Thank you for your continued support! 🥂", "Greetings")
        add("Thank You", "Hi {name}, we just wanted to say a massive THANK YOU for your recent purchase at {company}. Enjoy! 💖", "Greetings")
        add("Good Morning", "Good Morning {name}! Have a wonderful day ahead. - Your friends at {company}", "Greetings")

        // -- Festival Wishes --
        add("New Year", "Happy New Year {name}! 🎆 Wishing you health, wealth, and happiness in the coming year. - {company}", "Festival Wishes")
        add("Christmas", "Merry Christmas {name}! 🎄 May your holidays be filled with joy and laughter. Warm wishes from {company}.", "Festival Wishes")
        add("Diwali", "Happy Diwali {name}! 🪔 May the festival of lights bring prosperity and success to you and your family. - {company}", "Festival Wishes")
        add("Eid Mubarak", "Eid Mubarak {name}! 🌙 Wishing you a blessed and joyful Eid. - {company}", "Festival Wishes")
        add("Thanksgiving", "Happy Thanksgiving {name}! 🦃 We are so grateful for your support this year. Enjoy the holiday! - {company}", "Festival Wishes")
        add("Halloween", "Happy Halloween {name}! 🎃 No tricks, just a treat: get 15% off today at {company} with code SPOOKY15.", "Festival Wishes")

        // -- OTP --
        add("Standard OTP", "Your {company} verification code is: {custom}. Valid for {time} minutes. Do not share this code with anyone.", "OTP")
        add("Login Alert", "Hi {name}, a new login was detected on your {company} account from {city}. If this wasn't you, secure your account immediately.", "OTP")
        add("Password Reset", "{name}, click here to reset your {company} password: {custom}. Link expires in {time} minutes.", "OTP")
        add("Two-Factor Auth", "Your 2FA security code is {custom}. Enter this to complete your login at {company}.", "OTP")
        add("Account Verification", "Welcome {name}! Please verify your phone number by entering this code: {custom}. - {company}", "OTP")

        // -- Delivery --
        add("Order Confirmed", "Hi {name}, your order #{custom} has been confirmed by {company}. We will notify you when it ships!", "Delivery")
        add("Out for Delivery", "Good news {name}! Your package from {company} is out for delivery in {city} and will arrive today.", "Delivery")
        add("Delivered", "Hi {name}, your package from {company} has been delivered. We hope you love it! Tracking: {custom}", "Delivery")
        add("Delivery Delayed", "Dear {name}, your order #{custom} is experiencing a slight delay. New expected delivery is {date}. Sorry for the wait! - {company}", "Delivery")
        add("Ready for Pickup", "Hi {name}, your order is ready for pickup at our {city} store. Please bring your ID. - {company}", "Delivery")

        // -- Education --
        add("Class Reminder", "Hi {name}, a reminder that your next class for {custom} starts on {date} at {time}. Don't be late!", "Education")
        add("Exam Schedule", "Dear {name}, the exam schedule for {custom} has been published. Check the portal for details.", "Education")
        add("Fee Due", "Dear Parent, the tuition fee of {amount} for {name} is due on {date}. Please submit payment to the admin office.", "Education")
        add("Holiday Notice", "Notice: The campus will be closed on {date} for a local holiday. Classes resume the next day. - {company}", "Education")
        add("Event Invitation", "Hi {name}, join us for the annual science fair on {date} at {time}. Location: {city} Campus.", "Education")

        // -- Healthcare --
        add("Test Results", "Hi {name}, your recent test results from {company} are ready. Please log into your patient portal to view them: {custom}", "Healthcare")
        add("Vaccination Reminder", "Hi {name}, you are due for your upcoming vaccination on {date}. Call {phone} to schedule your visit.", "Healthcare")
        add("Prescription Ready", "Hello {name}, your prescription is ready for pickup at the {city} pharmacy. Total cost: {amount}.", "Healthcare")
        add("Health Checkup", "Dear {name}, it's been a while! Time for your annual health checkup at {company}. Book online: {custom}", "Healthcare")
        add("Clinic Relocation", "Notice: {company} has moved! Our new clinic is now located at {custom}, {city}. See you there!", "Healthcare")

        // -- Personal --
        add("Where are you?", "Hey {name}, where are you right now? Call me when you see this.", "Personal")
        add("Call me back", "Hi {name}, I tried calling but couldn't reach you. Please call me back at {phone} when free.", "Personal")
        add("Checking In", "Hey {name}! It's been a while since we caught up. Hope you're doing well. Let's grab coffee soon!", "Personal")
        add("Running Late", "Hi {name}, I'm running a bit late! Be there in about {time} minutes.", "Personal")
        add("Group Invite", "Hey {name}, we're hosting a small get-together on {date} at {time}. Hope you can make it!", "Personal")

        templateDao.insertTemplates(templates)
    }
}
