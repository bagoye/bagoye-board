import ResponseDto from "../Response.dto";

export default interface GetRelationListResponseDto extends ResponseDto {
    relativeWordList: string[];
}