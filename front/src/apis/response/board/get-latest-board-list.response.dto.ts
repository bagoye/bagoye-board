import { BoardListItem } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetLatestBoardListResponseDto extends ResponseDto {
    latestList: BoardListItem[];
}