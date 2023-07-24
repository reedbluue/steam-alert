package dev.ioliver.steamalert.dtos.steamProfileData;

public record SteamProfileDataDto(
        String personaname,
        Long steamid,
        String profileurl,
        String avatar
) {
}
