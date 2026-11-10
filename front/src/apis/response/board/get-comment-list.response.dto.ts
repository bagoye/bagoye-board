import { CommentListItem } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetCommentListResponseDto extends ResponseDto {
    commentList: CommentListItem[];
}