import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { capitalize, getInitials } from "@/lib/utils";
import { formatToTimeAgo } from "@/lib/utils";

interface ChatItemProps {
  username: string;
  message: string;
  date: string;
  isCurrentUser?: boolean;
  isGroup?: boolean;
  senderName?: string;
}

const ChatItem = ({
  username,
  message,
  date,
  isCurrentUser,
  isGroup,
  senderName,
}: ChatItemProps) => {
  return (
    <div className="flex items-center py-3 px-4 rounded-md hover:bg-muted/80 cursor-pointer gap-4">
      <Avatar className="h-12 w-12">
        <AvatarFallback>
          {getInitials(username ?? "Unknown User")}
        </AvatarFallback>
      </Avatar>
      <div className="flex flex-col flex-1">
        <div className="flex justify-between">
          <span className="font-semibold">
            {capitalize(username ?? "Unknown User")}
          </span>
          <span className="text-gray-700 dark:text-gray-500 text-[0.75rem] font-normal">
            {formatToTimeAgo(date)}
          </span>
        </div>
        <span className="text-gray-500 text-sm truncate">
          <span className="font-bold">
            {isGroup
              ? !isCurrentUser
                ? `${senderName}: `
                : "You: "
              : !isCurrentUser
              ? ""
              : "You: "}
          </span>
          {message}
        </span>
      </div>
    </div>
  );
};

export default ChatItem;
