import { FavoriteListItem } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetFavoriteListResponseDto extends ResponseDto {
    favoriteList: FavoriteListItem[]
}