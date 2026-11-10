import { BoardListItem } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetTop3BoardListResponseDto extends ResponseDto {
    top3List: BoardListItem[];
}